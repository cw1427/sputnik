package pl.touk.sputnik.review.locator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import pl.touk.sputnik.review.Review;
import pl.touk.sputnik.review.ReviewFile;

import javax.annotation.Nullable;
import java.util.List;

import static pl.touk.sputnik.review.Paths.SRC_MAIN;
import static pl.touk.sputnik.review.Paths.SRC_TEST;

public abstract class AbstractBuildDirLocator implements BuildDirLocator {

    public List<String> getBuildDirs(Review review) {
        return Lists.transform(review.getFiles(), new Function<ReviewFile, String>() {
            @Nullable
            @Override
            public String apply(ReviewFile file) {
                if (file.isSourceFile()) {
                    return StringUtils.substringBeforeLast(file.getReviewFilename(), SRC_MAIN).concat(getMainBuildDir());
                } else if (file.isTestFile()) {
                    return StringUtils.substringBeforeLast(file.getReviewFilename(), SRC_TEST).concat(getTestBuildDir());
                }
                return file.getSourceDir();
            }
        });
    }

    protected abstract String getMainBuildDir();

    protected abstract String getTestBuildDir();
}
