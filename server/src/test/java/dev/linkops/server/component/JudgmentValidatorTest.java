package dev.linkops.server.component;

import static org.junit.jupiter.api.Assertions.*;

import dev.linkops.server.dto.JudgmentResult;
import dev.linkops.server.dto.JudgmentResult.Answer;
import dev.linkops.server.dto.JudgmentResult.ChannelType;
import dev.linkops.server.dto.JudgmentResult.Level;
import dev.linkops.server.dto.JudgmentResult.Verdict;
import java.util.List;
import org.junit.jupiter.api.Test;

class JudgmentValidatorTest {

    private final JudgmentValidator validator = new JudgmentValidator();

    private static JudgmentResult result(Verdict v, Level l, Answer urlField) {
        return new JudgmentResult(v, l, ChannelType.DIRECTORY, urlField, Answer.NO, Answer.NO,
                List.of(), List.of("页面有 Website URL 字段"));
    }

    @Test
    void consistentResultPasses() {
        assertTrue(validator.validate(result(Verdict.SUITABLE, Level.HIGH, Answer.YES)).isEmpty());
        assertTrue(validator.validate(result(Verdict.UNCERTAIN, Level.UNRATED, Answer.UNKNOWN)).isEmpty());
    }

    @Test
    void uncertainMustBeUnratedAndViceVersa() {
        assertEquals(1, validator.validate(result(Verdict.UNCERTAIN, Level.MEDIUM, Answer.YES)).size());
        assertEquals(1, validator.validate(result(Verdict.UNSUITABLE, Level.UNRATED, Answer.YES)).size());
    }

    @Test
    void verdictAndLevelMustAgree() {
        assertFalse(validator.validate(result(Verdict.SUITABLE, Level.LOW, Answer.YES)).isEmpty());
        assertFalse(validator.validate(result(Verdict.UNSUITABLE, Level.HIGH, Answer.YES)).isEmpty());
    }

    @Test
    void suitableNeedsSomewhereToLeaveUrl() {
        assertFalse(validator.validate(result(Verdict.SUITABLE, Level.HIGH, Answer.NO)).isEmpty());
    }

    @Test
    void nullsAndBlankReasonsAreReported() {
        var r = new JudgmentResult(Verdict.SUITABLE, Level.HIGH, null, Answer.YES, Answer.NO, Answer.NO,
                null, List.of(" "));
        assertEquals(3, validator.validate(r).size());
    }
}
