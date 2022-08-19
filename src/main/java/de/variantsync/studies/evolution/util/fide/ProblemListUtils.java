package de.variantsync.studies.evolution.util.fide;

import de.variantsync.studies.evolution.util.functional.Result;
import de.ovgu.featureide.fm.core.io.Problem;
import de.ovgu.featureide.fm.core.io.ProblemList;

import java.util.function.Supplier;

public class ProblemListUtils {
    public static Exception toException(final ProblemList problems, final Supplier<String> errorMsg) {
        final StringBuilder sb = new StringBuilder();
        problems.stream().map(Problem::toString).forEach(s -> sb.append(s).append(" "));
        return new Exception(
                errorMsg.get() + System.lineSeparator() + "Reasons:" + System.lineSeparator() + sb
        );
    }

    public static <T> Result<T, Exception> toResult(final ProblemList problems, final Supplier<T> success, final Supplier<String> errorMsg) {
        if (problems.isEmpty()) {
            return Result.Success(success.get());
        } else {
            return Result.Failure(ProblemListUtils.toException(problems, errorMsg));
        }
    }
}
