package com.maddevs.server.repositories;

import com.maddevs.server.entities.SalesPo;
import com.maddevs.server.entities.SumPrice;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesPoRepository extends MongoRepository<SalesPo, String> {

 @Aggregation(pipeline = {
//   "{$project: {\n" +
//            "        month: {$month: $poDate},\n" +
//            "        year: {$year: $poDate},\n" +
//            "        amount: 1,\n" +
//            "        poDate: 1\n" +
//            "     }}"
//            , "{$match: {$and : [{year:?0} , {month:?1}] \n" +
//            "     }}"
//            ,
         "{$group: { \n" +
            "          '_id': {\n" +
            "            month: {$month: $poDate},\n" +
            "            year: {$year: $poDate} \n" +
            "          },\n" +
            "          totalPrice: {$sum: {$toDecimal:$amount}},\n" +
            "          }\n" +
            "      }"
            , "{$project: {\n" +
            "        _id: 0,\n" +
            "        totalPrice: {$toString: $totalPrice}\n" +
            "     }}"})
    AggregationResults<SumPrice> sumPriceThisYearMonth(Integer year, Integer month);
}
