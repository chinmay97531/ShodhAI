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
        if (contestRepository.count() > 0) {
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

    private TestCase testCase(Problem problem, String input, String expected) {
        TestCase testCase = new TestCase(input, expected, false);
        testCase.setProblem(problem);
        return testCase;
    }
}
