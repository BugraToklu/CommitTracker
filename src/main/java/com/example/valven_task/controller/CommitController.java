package com.example.valven_task.controller;

import com.example.valven_task.model.Developer;
import com.example.valven_task.repository.DeveloperRepository;
import com.example.valven_task.service.CommitService;
import com.example.valven_task.model.Commit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CommitController {
    private final CommitService commitService;
    private final DeveloperRepository developerRepository;


    @GetMapping("/commits")
    public String listCommits(Model model) {
        List<Commit> commits = commitService.getAllCommits();
        model.addAttribute("commits", commits);
        return "commit_list";
    }


    @GetMapping("/commits/{sha}")
    public String commitDetail(@PathVariable String sha, Model model) {
        Optional<Commit> commitOpt = commitService.getAllCommits().stream()
                .filter(c -> c.getSha().equals(sha))
                .findFirst();
        if (commitOpt.isPresent()) {
            model.addAttribute("commit", commitOpt.get());
            return "commit_detail";
        } else {
            model.addAttribute("errorMessage", "Commit not found");
            return "error";
        }
    }

    @GetMapping("/developer/{email}")
    public String developerCommits(@PathVariable String email, Model model) {
        Optional<Developer> developerOpt = developerRepository.findByEmail(email);

        if (developerOpt.isEmpty()) {
            model.addAttribute("errorMessage", "No developer found with email: " + email);
            return "error";
        }

        Developer developer = developerOpt.get();

        List<Commit> devCommits = commitService.getCommitsByDeveloperEmail(email);
        if (devCommits == null || devCommits.isEmpty()) {
            model.addAttribute("errorMessage", "No commits found for developer with email: " + email);
            return "error";
        }
        model.addAttribute("commits", devCommits);
        model.addAttribute("email", email);
        model.addAttribute("username", developer.getUsername());
        model.addAttribute("developer", developer);

        return "developer";
    }

}
