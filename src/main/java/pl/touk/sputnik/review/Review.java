package pl.touk.sputnik.review;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.touk.sputnik.review.filter.FileFilter;
import pl.touk.sputnik.review.transformer.FileTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Setter
@ToString
public class Review {
    private List<ReviewFile> files;
    private Map<Severity, Integer> violationCount = new EnumMap<>(Severity.class);
    private int totalViolationCount = 0;

    /**
     * Report problems with configuration, processors and other.
     * There problems should be displayed on review summary with your code-review tool
     *
     */
    private List<String> problems = new ArrayList<>();

    /**
     * Messages that will be displayed on review summary with your code-review tool
     */
    private List<String> messages = new ArrayList<>();
    private Map<String, Short> scores = new HashMap<>();

    private final ReviewFormatter formatter;

    public Review(@NotNull List<ReviewFile> files, ReviewFormatter reviewFormatter) {
        this.files = files;
        this.formatter = reviewFormatter;
    }

    @NotNull
    public <T> List<T> getFiles(@NotNull FileFilter fileFilter, @NotNull FileTransformer<T> fileTransformer) {
        return fileTransformer.transform(fileFilter.filter(files));
    }

    @NotNull
    public List<String> getSourceDirs() {
        return Lists.transform(files, new ReviewFileSourceDirFunction());
    }

    public void addProblem(@NotNull String source, @NotNull String problem) {
        problems.add(formatter.formatProblem(source, problem));
    }

    public void add(@NotNull String source, @NotNull ReviewResult reviewResult) {
        for (Violation violation : reviewResult.getViolations()) {
            addError(source, violation);
        }
    }

    public void addError(String source, Violation violation) {
        for (ReviewFile file : files) {
            if (separatorsToSystem(file.getReviewFilename()).equals(violation.getFilenameOrJavaClassName())
                    || separatorsToSystem(file.getIoFile().getAbsolutePath()).equals(violation.getFilenameOrJavaClassName())
                    || file.getJavaClassName().equals(violation.getFilenameOrJavaClassName())) {
                addError(file, source, violation.getLine(), violation.getMessage(), violation.getSeverity());
                return;
            }else{
                log.warn("Filename {} not equals to violation Filename {}",file.getReviewFilename(),violation.getFilenameOrJavaClassName());
            }
        }
        log.warn("Filename or Java class {} was not found in current review", violation.getFilenameOrJavaClassName());
    }

    public void printViolations(){
        for (ReviewFile file : files) {
            log.info("Total {} Violations on file {}", file.getComments().size(), file.getReviewFilename());
            for (Comment comm : file.getComments()){
                log.info("file: {} line: {} message: {}",file.getReviewFilename(),comm.getLine(), comm.getMessage());
            }
        }
    }

    private String separatorsToSystem(String res) {
        if (res==null) return null;
        if (File.separatorChar=='\\') {
            // From Windows to Linux/Mac
            return res.replace('/', File.separatorChar);
        } else {
            // From Linux/Mac to Windows
            return res.replace('\\', File.separatorChar);
        }
    }

    private void addError(@NotNull ReviewFile reviewFile, @NotNull String source, int line, @Nullable String message, Severity severity) {
        reviewFile.getComments().add(new Comment(line, formatter.formatComment(source, severity, message)));
        incrementCounters(severity);
    }

    private void incrementCounters(Severity severity) {
        totalViolationCount += 1;
        Integer currentCount = violationCount.get(severity);
        violationCount.put(severity, currentCount == null ? 1 : currentCount + 1);
    }

    @NoArgsConstructor
    private static class ReviewFileSourceDirFunction implements Function<ReviewFile, String> {

        @Override
        public String apply(ReviewFile from) {
            return from.getSourceDir();
        }
    }
}
