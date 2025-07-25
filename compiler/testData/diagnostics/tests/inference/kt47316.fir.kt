// RUN_PIPELINE_TILL: BACKEND
// FILE: GoBuildingRunningState.java
public abstract class GoBuildingRunningState<T extends GoBuildingRunConfiguration<? extends GoBuildingRunningState<T>>> implements RunProfileState {
    public String getBuildingWorkingDirectory() {
        return "";
    }
}

// FILE: GoBuildingRunConfiguration.java
public abstract class GoBuildingRunConfiguration<RunningState extends GoBuildingRunningState<? extends GoBuildingRunConfiguration<RunningState>>> { }

// FILE: RunProfileState.java
public interface RunProfileState {
}

// FILE: Test.java
import org.jetbrains.annotations.Nullable;

public class Test {
    @Nullable
    public RunProfileState getState() {
        return null;
    }
}

// FILE: main.kt
fun String.asFsdAddress(): String {
    return ""
}

fun box(): String {
    val state = Test().state
    if (state is GoBuildingRunningState<*>) {
        // Actually, that code is valid and met at IJ, but unfortunately it was broken since 1.7.0
        // See KT-52782 and testData/diagnostics/tests/inference/capturedTypes/kt52782.kt
        state.buildingWorkingDirectory.asFsdAddress()
    }
    return "OK"
}

/* GENERATED_FIR_TAGS: flexibleType, funWithExtensionReceiver, functionDeclaration, ifExpression, isExpression,
javaFunction, javaProperty, javaType, localProperty, nullableType, propertyDeclaration, smartcast, starProjection,
stringLiteral */
