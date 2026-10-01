package com.interviewarena.repository;

import com.interviewarena.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM questions ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Question> findRandom(@Param("count") int count);

    @Query(value = """
            SELECT * FROM questions
            WHERE (:category IS NULL OR category = :category)
              AND (:difficulty IS NULL OR difficulty = :difficulty)
            ORDER BY RAND()
            LIMIT :count
            """, nativeQuery = true)
    List<Question> findRandomFiltered(@Param("category") String category,
                                      @Param("difficulty") String difficulty,
                                      @Param("count") int count);
    @Query("select q.questionText from Question q")
    List<String> findAllQuestionTexts();
}