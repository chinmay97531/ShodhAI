package ai.shodhai.judge.config;

import ai.shodhai.judge.domain.Contest;
import ai.shodhai.judge.domain.Problem;
import ai.shodhai.judge.domain.TestCase;
import ai.shodhai.judge.repository.ContestRepository;
import ai.shodhai.judge.repository.ProblemRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;

    public DataInitializer(ContestRepository contestRepository, ProblemRepository problemRepository) {
        this.contestRepository = contestRepository;
        this.problemRepository = problemRepository;
    }

    @PostConstruct
    @Transactional
    public void populate() {
        createWinterOpenContestIfMissing();
        createSodhAiContestIfMissing();
    }

    private TestCase testCase(Problem problem, String input, String expected) {
        TestCase testCase = new TestCase(input, expected, false);
        testCase.setProblem(problem);
        return testCase;
    }

    private void createWinterOpenContestIfMissing() {
        if (contestRepository.existsById("winter-open")) {
            return;
        }

        Contest contest = new Contest("winter-open", "Winter Open", "A sample programming contest to demonstrate the live judge.");
        contestRepository.save(contest);

        Problem sumProblem = new Problem("warmup-sum", "Warm-up Sum", "Read two integers and output their sum.", "Easy");
        sumProblem.setContest(contest);
        sumProblem.getTestCases().add(testCase(sumProblem, "1 2\n", "3"));
        sumProblem.getTestCases().add(testCase(sumProblem, "10 32\n", "42"));

        Problem fizzProblem = new Problem("fizz-buzz", "Fizz Buzz", "Print numbers from 1 to N replacing multiples of 3 with Fizz and 5 with Buzz.", "Easy");
        fizzProblem.setContest(contest);
        fizzProblem.getTestCases().add(testCase(fizzProblem, "5\n", "1\n2\nFizz\n4\nBuzz"));

        Problem palindromeProblem = new Problem("palindrome-check", "Palindrome", "Determine if the input string is a palindrome.", "Medium");
        palindromeProblem.setContest(contest);
        palindromeProblem.getTestCases().add(testCase(palindromeProblem, "racecar\n", "YES"));
        palindromeProblem.getTestCases().add(testCase(palindromeProblem, "hello\n", "NO"));

        problemRepository.save(sumProblem);
        problemRepository.save(fizzProblem);
        problemRepository.save(palindromeProblem);
    }

    private void createSodhAiContestIfMissing() {
        if (contestRepository.existsById("sodh-ai-contest")) {
            return;
        }

        Contest contest = new Contest("sodh-ai-contest", "Sodh AI Contest", "Challenge set featuring algorithmic puzzles curated for Shodh AI participants.");
        contestRepository.save(contest);

        Problem twoSum = new Problem(
                "two-sum",
                "Two Sum",
                "Given an array of integers and a target value, find the indices (1-indexed) of the two numbers whose sum equals the target."
                        + "\nInput:\n- First line contains n, the number of elements.\n- Second line contains n space-separated integers.\n- Third line contains the target value.\nOutput:\n- Two integers representing the 1-indexed positions of the numbers in ascending order.",
                "Easy");
        twoSum.setContest(contest);
        twoSum.getTestCases().add(testCase(twoSum, "4\n2 7 11 15\n9\n", "1 2"));
        twoSum.getTestCases().add(testCase(twoSum, "3\n3 2 4\n6\n", "2 3"));

        Problem nextGreater = new Problem(
                "next-greater-element",
                "Next Greater Element",
                "For each element in the array, output the first greater element to its right or -1 if none exists."
                        + "\nInput:\n- First line contains n.\n- Second line contains n space-separated integers.\nOutput:\n- n space-separated integers where the i-th value is the next greater element for the i-th input value.",
                "Medium");
        nextGreater.setContest(contest);
        nextGreater.getTestCases().add(testCase(nextGreater, "5\n2 1 2 4 3\n", "4 2 4 -1 -1"));
        nextGreater.getTestCases().add(testCase(nextGreater, "4\n1 3 2 4\n", "3 4 4 -1"));

        Problem dungeon = new Problem(
                "dungeon-problem",
                "Dungeon Rescue",
                "Navigate a dungeon represented as a grid of health changes and determine the minimum initial health required to reach the bottom-right cell without the health dropping to zero or below at any point."
                        + "\nInput:\n- First line contains two integers r and c.\n- Next r lines contain c space-separated integers representing the dungeon.\nOutput:\n- A single integer denoting the minimum initial health needed.",
                "Hard");
        dungeon.setContest(contest);
        dungeon.getTestCases().add(testCase(dungeon, "3 3\n0 -2 -3\n1 -5 -10\n4 -5 -1\n", "7"));
        dungeon.getTestCases().add(testCase(dungeon, "2 2\n-2 -3\n-5 -10\n", "6"));

        problemRepository.save(twoSum);
        problemRepository.save(nextGreater);
        problemRepository.save(dungeon);
    }
}
