package com.javamentor.qa.platform.webapp.controllers.rest;

import com.javamentor.qa.platform.models.dto.AnswerCreateDto;
import com.javamentor.qa.platform.models.dto.AnswerDto;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.VoteAnswer;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.service.abstracts.dto.AnswerDtoService;
import com.javamentor.qa.platform.service.abstracts.dto.UserDtoService;
import com.javamentor.qa.platform.service.abstracts.model.AnswerService;
import com.javamentor.qa.platform.service.abstracts.model.QuestionService;
import com.javamentor.qa.platform.service.abstracts.model.ReputationService;
import com.javamentor.qa.platform.service.abstracts.model.VoteOnAnswerService;
import com.javamentor.qa.platform.webapp.converters.AnswerConverter;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.javamentor.qa.platform.models.entity.question.answer.VoteType.DOWN_VOTE;
import static com.javamentor.qa.platform.models.entity.question.answer.VoteType.UP_VOTE;


@RestController
@Api("Answer Api")
@RequestMapping("api/user/question")
public class AnswerResourceController {

    private final AnswerDtoService answerDtoService;
    private final AnswerService answerService;
    private final UserDtoService userDtoService;
    private final QuestionService questionService;
    private final VoteOnAnswerService voteOnAnswerService;
    private final ReputationService reputationService;
    private final AnswerConverter answerConverter;

    @Autowired
    public AnswerResourceController(
            AnswerDtoService answerDtoService,
            AnswerService answerService,
            UserDtoService userDtoService,
            QuestionService questionService,
            VoteOnAnswerService voteOnAnswerService,
            ReputationService reputationService,
            AnswerConverter answerConverter ) {
        this.answerDtoService = answerDtoService;
        this.answerService = answerService;
        this.userDtoService = userDtoService;
        this.questionService = questionService;
        this.voteOnAnswerService = voteOnAnswerService;
        this.reputationService = reputationService;
        this.answerConverter = answerConverter;
    }

    @GetMapping("/{questionId}/answer")
    @ApiOperation(
            value = "Returns List of AnswerDtos corresponding questionId",
            notes = "Returns HTTP 404 if the questionId is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid List of AnswerDtos found"),
            @ApiResponse(code = 404, message = "Answers with id not found")})
    public ResponseEntity<?> getAllAnswerByQuestionId(@PathVariable("questionId") Long id) {
        List<AnswerDto> answerDtos = answerDtoService.getAllAnswersByQuestionId(id);

        return answerDtos.isEmpty() ?
                new ResponseEntity<>("Answers with id " + id + " not found!", HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(answerDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{questionId}/answer/{answerId}")
    @ApiOperation(value = "???????????????? ???????????? answerId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid  Answer ID found"),
            @ApiResponse(code = 404, message = "Answer ID with id not found"),
            @ApiResponse(code = 400, message = "Invalid Answer ID entry")})
    public ResponseEntity<?> deleteAnswerById(@ApiParam(name = "answerId") @PathVariable Long answerId) {
        if (answerId == null) {
            return ResponseEntity.badRequest().body("Error deleting an answer Id: " + answerId);
        } if (answerService.existsById(answerId)) {
            answerService.deleteById(answerId);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity("Answer Id " + answerId + " not found!", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{questionId}/answer/{id}/upVote")
    @ApiOperation(value = "???????????? ?? ???? ?????????????????????? ???? ?????????? ???? ?????????????????? UP")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "???????????????? ?????????????????? ???????????? ??????????????"),
            @ApiResponse(code = 400, message = "???????????? ??????????????????????")})
    public ResponseEntity<?> insertUpVote(@PathVariable("questionId") Long questionId, @PathVariable("id") Long answerId) {
        User sender = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Optional<Answer> optionalAnswer = answerService.getById(answerId);
        if (optionalAnswer.isPresent()) {
            Answer answer = optionalAnswer.get();
            if (!(voteOnAnswerService.getIfNotExists(answer.getId(), sender.getId()))) {
                VoteAnswer upVoteAnswer = new VoteAnswer(sender, answer, UP_VOTE);
                voteOnAnswerService.persist(upVoteAnswer);
                return new ResponseEntity<>(voteOnAnswerService.getCountOfVotes(answerId), HttpStatus.OK);
            }
            return new ResponseEntity<>("?????? ?????????? ?????? ??????????", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("???????????? answer ???? ????????????????????", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{questionId}/answer/{id}/downVote")
    @ApiOperation(value = "???????????? ?? ???? ?????????????????????? ???? ?????????? ???? ?????????????????? Down")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "?????????????????? ?????????????????? ???????????? ??????????????"),
            @ApiResponse(code = 400, message = "???????????? ??????????????????????")})
    public ResponseEntity<?> insertDownVote(@PathVariable("questionId") Long questionId, @PathVariable("id") Long answerId) {
        User sender = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Optional<Answer> optionalAnswer = answerService.getById(answerId);
        if (optionalAnswer.isPresent()) {
            Answer answer = optionalAnswer.get();
            if (!(voteOnAnswerService.getIfNotExists(answer.getId(), sender.getId()))) {
                VoteAnswer downVoteAnswer = new VoteAnswer(sender, answer, DOWN_VOTE);
                voteOnAnswerService.persist(downVoteAnswer);
                return new ResponseEntity<>(voteOnAnswerService.getCountOfVotes(answerId), HttpStatus.OK);
            }
            return new ResponseEntity<>("?????? ?????????? ?????? ??????????", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("???????????? answer ???? ????????????????????", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{questionId}/answer/add")
    @ApiOperation(value = "???????????????????? ???????????? ?? ??????????????")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "?????????? ????????????????"),
            @ApiResponse(code = 400, message = "???????????? ???????????????????? ??????????????")})
    public ResponseEntity<?> addAnswerByQuestionId(@Valid @RequestBody AnswerCreateDto answerCreateDto, @PathVariable("questionId") Long questionId) {

        User sender = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Optional<Question> optionalQuestion = questionService.getById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            if (answerService.getIfNotExists(question.getId(), sender.getId())) {
                Answer answer = new Answer();
                answer.setHtmlBody(answerCreateDto.getBody());
                answer.setUser(sender);
                answer.setQuestion(question);
                answerService.persist(answer);
                return new ResponseEntity<>(answerConverter.answerToAnswerDto(answer), HttpStatus.OK);
            }
            return new ResponseEntity<>("???? ?????? ???????????????? ???? ???????????? ????????????", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("?????????????? ?? ?????????????????? id, ???? ????????????????????", HttpStatus.NOT_FOUND);
    }

}