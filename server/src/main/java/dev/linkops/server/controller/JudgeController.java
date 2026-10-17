package dev.linkops.server.controller;

import dev.linkops.server.judge.JudgeRequest;
import dev.linkops.server.judge.JudgeResponse;
import dev.linkops.server.judge.JudgeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/judge")
public class JudgeController {

    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    /** 对单个外链入口做研判预览,不写库。 */
    @PostMapping("/preview")
    public JudgeResponse preview(@RequestBody JudgeRequest request) {
        return judgeService.preview(request);
    }
}
