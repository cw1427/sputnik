package pl.touk.sputnik.processor.findbugs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import pl.touk.sputnik.TestEnvironment;
import pl.touk.sputnik.configuration.ConfigurationSetup;
import pl.touk.sputnik.configuration.GeneralOption;
import pl.touk.sputnik.review.Review;
import pl.touk.sputnik.review.ReviewException;
import pl.touk.sputnik.review.ReviewFile;
import pl.touk.sputnik.review.ReviewResult;

import java.util.List;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.touk.sputnik.di.DIContext.context;

@RunWith(MockitoJUnitRunner.class)
public class FindBugsProcessorTest extends TestEnvironment {

    private FindBugsProcessor findBugsProcessor;

    @Before
    public void setUp() throws Exception {
        new ConfigurationSetup().setUp(ImmutableMap.of(GeneralOption.BUILD_TOOL.getKey(), "gradle"));
        findBugsProcessor = new FindBugsProcessor(context());
    }

    @Test
    public void shouldReturnBasicViolationsOnEmptyClass() {
        //given
        List<ReviewFile> files = ImmutableList.of(new ReviewFile("src/test/java/toreview/TestClass.java"));
        Review review = new Review(files);

        //when
        ReviewResult reviewResult = findBugsProcessor.process(review);

        //then
        assertThat(reviewResult).isNotNull();
        assertThat(reviewResult.getViolations())
                .isNotEmpty()
                .hasSize(2)
                .extracting("message")
                .containsOnly(
                        "DLS: Dead store to value in toreview.TestClass.incorrectAssignmentInIfCondition()",
                        "QBA: toreview.TestClass.incorrectAssignmentInIfCondition() assigns boolean literal in boolean expression"
                );
    }

    @Test
    public void shouldThrowWhenFileNotFound() {
        //when
        catchException(findBugsProcessor).process(nonexistantReview());

        //then
        assertThat(caughtException()).isInstanceOf(ReviewException.class);
    }

}
