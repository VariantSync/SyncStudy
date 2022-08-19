package de.variantsync.studies.evolution.util.functional;

import java.util.Optional;

public class Traversable {
    public static <S, F> Result<Optional<S>, F> sequence(final Optional<Result<S, F>> o) {
        return Functional.match(o,
                just -> just.map(Optional::of),
                () -> Result.Success(Optional.empty()));
    }
}
