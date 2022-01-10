package com.javamentor.qa.platform.dao.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.QuestionDtoDao;
import com.javamentor.qa.platform.dao.util.SingleResultUtil;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import com.javamentor.qa.platform.models.dto.TagDto;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Ali Veliev 10.12.2021
 */

@Repository
public class QuestionDtoDaoImpl implements QuestionDtoDao {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public Optional<QuestionDto> getQuestionById(Long id) {

        Optional<QuestionDto> questionDto = SingleResultUtil.getSingleResultOrNull(entityManager.createNativeQuery("select q.id as q_id, q.title, q.description,  q.last_redaction_date,  q.persist_date,  u.id as u_id,  u.full_name,  u.image_link, " +
                        "(select sum(r.count) from reputation r where r.author_id = u.id) as reputation, " +
                        "(select count(up.vote) from votes_on_questions up where up.vote = 'UP_VOTE' and up.question_id = q.id) - (select count(down.vote)   from votes_on_questions down   where down.vote = 'DOWN_VOTE' and down.question_id = q.id)      as votes, " +
                        "(select count(a.id) from answer a where a.question_id = q.id)    as answers, " +
                        "t.id as t_id, t.name as t_name, t.description as t_desc " +
                        "from question q join user_entity u on u.id = q.user_id " +
                        "join question_has_tag qht on q.id = qht.question_id " +
                        "join tag t on qht.tag_id = t.id where q.id =:id")
                .setParameter("id", id)
                .unwrap(Query.class)
                .setResultTransformer(new ResultTransformer() {

                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        QuestionDto questionDto = new QuestionDto();
                        TagDto tagDto = new TagDto();
                        tagDto.setId(((BigInteger) tuple[11]).longValue());
                        tagDto.setName((String) tuple[12]);
                        tagDto.setDescription((String) tuple[13]);
                        List<TagDto> tagDtoList = new ArrayList<>();
                        tagDtoList.add(tagDto);
                        questionDto.setId(((BigInteger) tuple[0]).longValue());
                        questionDto.setTitle((String) tuple[1]);
                        questionDto.setDescription((String) tuple[2]);
                        questionDto.setLastUpdateDateTime(((Timestamp) tuple[3]).toLocalDateTime());
                        questionDto.setPersistDateTime(((Timestamp) tuple[4]).toLocalDateTime());
                        questionDto.setAuthorId(((BigInteger) tuple[5]).longValue());
                        questionDto.setAuthorName((String) tuple[6]);
                        questionDto.setAuthorImage((String) tuple[7]);
                        questionDto.setAuthorReputation(((BigInteger) tuple[8]).longValue());
                        questionDto.setCountValuable(((BigInteger) tuple[9]).intValue());
                        questionDto.setCountAnswer(((BigInteger) tuple[10]).intValue());
                        questionDto.setViewCount(0);
                        questionDto.setListTagDto(tagDtoList);
                        return questionDto;
                    }

                    @Override
                    public List transformList(List list) {

                        List<TagDto> tagDtoList = new ArrayList<>();

                        for (Object a : list) {
                            tagDtoList.add(((QuestionDto) a).getListTagDto().get(0));
                        }

                        QuestionDto questionDto = (QuestionDto) list.get(0);
                        questionDto.setListTagDto(tagDtoList);
                        for (int i = list.size() - 1; i != 0; i--) {
                            list.remove(i);
                        }
                        return list;
                    }
                }));
        return questionDto;
    }
}