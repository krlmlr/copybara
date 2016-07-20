import static com.google.copybara.testing.LogSubjects.assertThatConsole;
import com.google.copybara.Destination;
import com.google.copybara.Destination.WriterResult;
import com.google.copybara.util.console.testing.TestingConsole;
import com.google.copybara.util.console.testing.TestingConsole.MessageType;
  private TestingConsole console;
    console = new TestingConsole();
    options = new OptionsBuilder().setConsole(console);
    return destinationFirstCommit(/*askConfirmation*/ false);
  }

  private GitDestination destinationFirstCommit(boolean askConfirmation)
      throws ConfigValidationException {
    return yaml.withOptions(options.build(), CONFIG_NAME, askConfirmation);
    return yaml.withOptions(options.build(), CONFIG_NAME, /*askConfirmation*/ false);
      throws RepoException, ConfigValidationException, IOException {
    WriterResult destinationResult = destination.newWriter().write(result, console);
    assertThat(destinationResult).isEqualTo(WriterResult.OK);
  @Test
  public void processUserAborts() throws Exception {
    console = new TestingConsole()
        .respondNo();
    yaml.setFetch("master");
    yaml.setPush("master");
    Files.write(workdir.resolve("test.txt"), "some content".getBytes());
    thrown.expect(RepoException.class);
    thrown.expectMessage("User aborted execution: did not confirm diff changes");
    process(destinationFirstCommit(/*askConfirmation*/ true), new DummyReference("origin_ref"));
  }

  @Test
  public void processUserConfirms() throws Exception {
    console = new TestingConsole()
        .respondYes();
    yaml.setFetch("master");
    yaml.setPush("master");
    Files.write(workdir.resolve("test.txt"), "some content".getBytes());
    process(destinationFirstCommit(/*askConfirmation*/ true), new DummyReference("origin_ref"));

    assertThatConsole(console)
        .matchesNext(MessageType.PROGRESS, "Git Destination: Fetching file:.*")
        .matchesNext(MessageType.PROGRESS, "Git Destination: Adding files for push")
        .equalsNext(MessageType.INFO, "\n"
            + "diff --git a/test.txt b/test.txt\n"
            + "new file mode 100644\n"
            + "index 0000000..f0eec86\n"
            + "--- /dev/null\n"
            + "+++ b/test.txt\n"
            + "@@ -0,0 +1 @@\n"
            + "+some content\n"
            + "\\ No newline at end of file\n")
        .matchesNext(MessageType.WARNING, "Proceed with push to.*[?]")
        .matchesNext(MessageType.PROGRESS, "Git Destination: Pushing to .*")
        .containsNoMoreMessages();
  }


  @Test
  public void pushSequenceOfChangesToReviewBranch() throws Exception {
    yaml.setFetch("master");
    yaml.setPush("refs_for_master");

    Destination.Writer writer = destinationFirstCommit().newWriter();

    Files.write(workdir.resolve("test42"), "42".getBytes(UTF_8));
    WriterResult result = writer.write(TransformResults.of(workdir, new DummyReference("ref1")), console);
    assertThat(result).isEqualTo(WriterResult.OK);
    String firstCommitHash = repo().simpleCommand("rev-parse", "refs_for_master").getStdout();

    Files.write(workdir.resolve("test99"), "99".getBytes(UTF_8));
    result = writer.write(TransformResults.of(workdir, new DummyReference("ref2")), console);
    assertThat(result).isEqualTo(WriterResult.OK);

    // Make sure parent of second commit is the first commit.
    assertThat(repo().simpleCommand("rev-parse", "refs_for_master~1").getStdout())
        .isEqualTo(firstCommitHash);

    // Make sure commits have correct file content.
    GitTesting.assertThatCheckout(repo(), "refs_for_master~1")
        .containsFile("test42", "42")
        .containsNoMoreFiles();
    GitTesting.assertThatCheckout(repo(), "refs_for_master")
        .containsFile("test42", "42")
        .containsFile("test99", "99")
        .containsNoMoreFiles();
  }