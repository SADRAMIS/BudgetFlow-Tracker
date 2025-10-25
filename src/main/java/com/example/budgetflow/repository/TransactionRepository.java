package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    // Получить все транзакции пользователя
    List<Transaction> findByUserId(Long userId);

    // Получить транзакции пользователя по типу (INCOME/EXPENSE)
    List<Transaction> findByUserIdAndType(Long userId,String type);

    // Получить транзакции пользователя за период
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate,LocalDate endDate);

    List<Transaction> findByCategoryId(Long categoryId);

    // Кастомный запрос: сумма всех транзакций пользователя по типу
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    Double sumAmountByUserIdAndType(@Param("userId") Long userId,
                                    @Param("type")String type);

    // Получить транзакции пользователя за месяц
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    List<Transaction> findByUserIdAndYearAndMonth(@Param("userId") Long userId,
                                                  @Param("year") int year,
                                                  @Param("month") int month);

}
