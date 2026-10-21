package hr.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import hr.dto.ApprovalRequestDTO;
import hr.entity.ApprovalDocEntity;
import hr.service.ApprovalService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    // 1. 기안서 작성 폼 페이지
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("requestDTO", new ApprovalRequestDTO());
        return "approval-form";
    }

    // 2. 기안서 제출 처리 (사원 누구나 가능)
    @PostMapping("/new")
    public String submitForm(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute ApprovalRequestDTO dto) {
        approvalService.createDoc(userDetails.getUsername(), dto);
        return "redirect:/approval/list";
    }

    // 3. 결재 문서 목록 조회 페이지
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("docs", approvalService.getAllDocs());
        return "approval-list";
    }

    // 4. 결재 승인 처리 (관리자 ROLE_ADMIN 또는 팀장 ROLE_MANAGER만 허용)
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable("id") Long id) {
        approvalService.approveDoc(id);
        return "redirect:/approval/list";
    }
    
    // 5. 공문서 A4 상세 보기 페이지 (기안자, 팀장 모두 열람 가능)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        ApprovalDocEntity doc = approvalService.getDocById(id);
        model.addAttribute("doc", doc);
        return "approval-detail";
    }
}