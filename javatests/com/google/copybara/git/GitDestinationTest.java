import com.google.copybara.testing.DummyOriginalAuthor;
import com.google.copybara.util.PathMatcherBuilder;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
  private GitDestination destinationFirstCommit()
    return yaml.withOptions(options.build(), CONFIG_NAME);
    return yaml.withOptions(options.build(), CONFIG_NAME /*askConfirmation*/);
    processWithBaselineAndConfirmation(destination, originRef, baseline,
        /*askForConfirmation*/false);
  }

  private void processWithBaselineAndConfirmation(GitDestination destination,
      DummyReference originRef,
      String baseline, boolean askForConfirmation)
      throws ConfigValidationException, RepoException, IOException {
    TransformResult result = TransformResults.of(workdir,
        originRef,
        PathMatcherBuilder.create(FileSystems.getDefault(), excludedDestinationPaths,
            ImmutableList.<String>of()));

    if (askForConfirmation) {
      result = result.withAskForConfirmation(true);
    }
    processWithBaselineAndConfirmation(destinationFirstCommit(), new DummyReference("origin_ref"),
        /*baseline=*/null, /*askForConfirmation=*/true);
    processWithBaselineAndConfirmation(destinationFirstCommit(), new DummyReference("origin_ref"),
        /*baseline=*/null, /*askForConfirmation=*/true);
    console.assertThat()
        .withOriginalAuthor(new DummyOriginalAuthor("Foo Bar", "foo@bar.com"))