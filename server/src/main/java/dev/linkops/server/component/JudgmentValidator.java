package dev.linkops.server.component;

import dev.linkops.server.dto.JudgmentResult;
import dev.linkops.server.dto.JudgmentResult.Answer;
import dev.linkops.server.dto.JudgmentResult.Level;
import dev.linkops.server.dto.JudgmentResult.Verdict;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 业务规则校验。Spring AI 的 validateSchema() 只保证 JSON 结构对;
 * 字段之间是否矛盾、是否违反 judge-system.st 的规则,需要我们自己校验。
 *
 * 返回违规清单而不是抛异常:每条信息会作为反馈发回给模型重试,所以要写成模型能照着改的话。
 */
@Component
public class JudgmentValidator {

    public List<String> validate(JudgmentResult r) {
        List<String> errors = new ArrayList<>();
        if (r == null) {
            errors.add("结果为空");
            return errors;
        }

        // 必填:schema 要求字段存在,但模型仍可能填 null
        if (r.verdict() == null) errors.add("verdict 不能为空");
        if (r.level() == null) errors.add("level 不能为空");
        if (r.channelType() == null) errors.add("channelType 不能为空,看不出来时填 UNKNOWN");
        if (r.hasUrlField() == null) errors.add("hasUrlField 不能为空,看不出来时填 UNKNOWN");
        if (r.requiresLogin() == null) errors.add("requiresLogin 不能为空,看不出来时填 UNKNOWN");
        if (r.requiresPayment() == null) errors.add("requiresPayment 不能为空,看不出来时填 UNKNOWN");
        if (r.risks() == null) errors.add("risks 不能为空,没有风险时填空数组 []");
        if (r.reasons() == null || r.reasons().isEmpty()) {
            errors.add("reasons 至少要有一条");
        } else if (r.reasons().stream().anyMatch(s -> s == null || s.isBlank())) {
            errors.add("reasons 中不能有空字符串");
        }
        if (r.verdict() == null || r.level() == null) return errors;

        // 结论与档位必须一致(对应打分规则与"不确定就明确标记")
        boolean uncertain = r.verdict() == Verdict.UNCERTAIN;
        boolean unrated = r.level() == Level.UNRATED;
        if (uncertain && !unrated) errors.add("verdict 为 UNCERTAIN 时,level 必须为 UNRATED");
        if (!uncertain && unrated) errors.add("level 为 UNRATED 时,verdict 必须为 UNCERTAIN");
        if (r.verdict() == Verdict.SUITABLE && r.level() == Level.LOW) {
            errors.add("verdict 为 SUITABLE 时 level 不能是 LOW:低档表示没有相关度");
        }
        if (r.verdict() == Verdict.UNSUITABLE && r.level() == Level.HIGH) {
            errors.add("verdict 为 UNSUITABLE 时 level 不能是 HIGH:请检查结论或档位");
        }

        // 没有地方留网址,就谈不上适合
        if (r.verdict() == Verdict.SUITABLE && r.hasUrlField() == Answer.NO) {
            errors.add("hasUrlField 为 NO 时不能判 SUITABLE:页面没有可以留网址的位置");
        }
        return errors;
    }
}
