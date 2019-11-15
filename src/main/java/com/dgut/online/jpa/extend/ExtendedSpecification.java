package com.dgut.online.jpa.extend;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.ParameterExpression;
import java.util.Collection;
import java.util.Map;

/**
 * @ClassName ExtendedSpecification
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/12 0012
 * @Version V1.0
 **/
public interface ExtendedSpecification<T> extends Specification<T> {
    /**
     * 返回构造 in 范围查询的字段参数的map集合，key是字段名fieldName
     * @return
     */
    default Map<String, ParameterExpression<Collection<?>>> getInParameterMap(){
        return null;
    }

    /**
     * 返回in范围查询字段的查找值的map集合，key是字段名fieldName，value是范围集合
     * @return
     */
    default Map<String,Collection<?>> getInParameterValueMap(){
        return null;
    }

    /**
     * 返回一个结果字段建造器
     * @return
     */
    default SelectorBuilder getSelectorBuilder(){
        return null;
    }

    /**
     * 是否有in查找字段
     * @return
     */
    default boolean hasInCondition(){
        return false;
    }

}
