package cern.ais.gridwars.runtime;

import java.util.Arrays;


public enum MatchRuntimeResult {

    BOT1_WINNER(-1), BOT2_WINNER(-2), DRAW(0), ERROR(1), TIMEOUT(2), UNKNOWN_STATUS(3);

    final int returnCode;

    MatchRuntimeResult(int returnCode) {
        this.returnCode = returnCode;
    }

    public static MatchRuntimeResult fromReturnCode(int returnCode) {
        return Arrays.stream(values())
            .filter(result -> result.returnCode == returnCode)
            .findFirst()
            .orElse(UNKNOWN_STATUS);
    }
}
