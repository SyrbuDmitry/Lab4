package akke.remotejstest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Test {
    private String testName;
    private String expectedResult;
    public Object[] params;
    Test(@JsonProperty("testName") String testName,
         @JsonProperty("expectedResult") String expectedResult,
         @JsonProperty("params") Object[] params){
        this.testName = testName;
        this.expectedResult = expectedResult;
        this.params = params;
    }
}
