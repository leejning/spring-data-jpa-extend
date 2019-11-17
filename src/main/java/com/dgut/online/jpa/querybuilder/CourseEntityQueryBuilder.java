package com.dgut.online.jpa.querybuilder;


import com.dgut.online.jpa.bean.Course;
import com.dgut.online.jpa.extend.BaseEntityQueryBuilder;
import com.dgut.online.jpa.extend.ExtendedSpecification;
import com.dgut.online.jpa.extend.SelectorBuilder;
import lombok.Data;
import org.assertj.core.util.Lists;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * @ClassName CourseEntityQueryBuilder
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/14 0014
 * @Version V1.0
 **/
@Data
public class CourseEntityQueryBuilder extends BaseEntityQueryBuilder {


    public static ExtendedSpecification<Course> getWhere(CourseEntityQueryBuilder queryDTO) {
        return new ExtendedSpecification<Course>() {
            //有范围查询要使用这两个属性，
            Map<String, ParameterExpression<Collection<?>>> ipm;
            Map<String, Collection<?>> ipvm;

            @Override
            public Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = Lists.newArrayList();
                if(queryDTO.isWithIn()){
                    Path<?> path = root.get(queryDTO.getInField());
                    ParameterExpression<Collection<?>> parameter =
                            (ParameterExpression<Collection<?>>) (ParameterExpression) criteriaBuilder.parameter(Collection.class);
                    ipm = new HashMap<>();
                    ipm.put(queryDTO.getInField(), parameter);
                    ipvm = new HashMap<>();
                    ipvm.put(queryDTO.getInField(), queryDTO.getInValue());
                    predicates.add(path.in(parameter));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }

            @Override
            public Map<String, ParameterExpression<Collection<?>>> getInParameterMap() {
                return ipm;
            }
            @Override
            public Map<String, Collection<?>> getInParameterValueMap() {
                return ipvm;
            }

            @Override
            public SelectorBuilder getSelectorBuilder() {
                SelectorBuilder selectorBuilder = new SelectorBuilder();
                String[] fieldNames = {"id","courseName","courseType","teacherName"};
                if(queryDTO.getResultClass()!=null){
                    fieldNames = initResultFields(queryDTO.getResultClass());
                }
                return selectorBuilder.append(fieldNames);
            }

            @Override
            public boolean hasInCondition() {
                return queryDTO.isWithIn();
            }
        };
    }
}
