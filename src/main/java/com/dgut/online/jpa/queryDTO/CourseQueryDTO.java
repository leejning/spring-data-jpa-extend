package com.dgut.online.jpa.queryDTO;


import com.dgut.online.jpa.bean.Course;
import com.dgut.online.jpa.extend.ExtendedSpecification;
import com.dgut.online.jpa.extend.SelectorBuilder;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * @ClassName CourseQueryDTO
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/14 0014
 * @Version V1.0
 **/
@Data
public class CourseQueryDTO {

    private boolean withIn;
    private String inField;
    private List<String> inValue = Collections.EMPTY_LIST;

    public static ExtendedSpecification<Course> getWhere(CourseQueryDTO queryDTO) {
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
                selectorBuilder.append("id","courseName","courseType","teacherName");
                return selectorBuilder;
            }

            @Override
            public boolean hasInCondition() {
                return queryDTO.isWithIn();
            }
        };
    }
}
