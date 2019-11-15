package org.springframework.data.jpa.repository.support;

import com.dgut.online.jpa.extend.ExtendedJpaRepositoryApi;
import com.dgut.online.jpa.extend.ExtendedSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * @ClassName ExtendedJpaRepository
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/12 0012
 * @Version V1.0
 **/
public class ExtendedJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> implements ExtendedJpaRepositoryApi<T, ID> {
    private EntityManager entityManager;
    private JpaEntityInformation<T, ?> entityInformation;

    public ExtendedJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    public List<T> findAll(ExtendedSpecification<T> spec) {
        return getQuery(spec, Sort.unsorted()).getResultList();
    }

    @Override
    public List<T> findAll(ExtendedSpecification<T> spec, Sort sort) {
        return getQuery(spec, sort).getResultList();
    }

    @Override
    public Page<T> findAll(ExtendedSpecification<T> spec, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        return pageable.isUnpaged() ? new PageImpl<T>(query.getResultList())
                : readPage(query, getDomainClass(), pageable, spec);
    }

    /**
     * 重载方法 SimpleJpaRepository的方法
     *
     * @param spec
     * @param pageable
     * @return
     */
    protected TypedQuery<T> getQuery(@Nullable ExtendedSpecification<T> spec, Pageable pageable) {
        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getQuery(spec, sort);
    }

    /**
     * 重载方法 SimpleJpaRepository的方法
     *
     * @param spec
     * @param sort
     * @return
     */
    protected TypedQuery<T> getQuery(@Nullable ExtendedSpecification<T> spec, Sort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(getDomainClass());

        Root<T> root = query.from(getDomainClass());

        /**
         * 构造select...
         */
        if (spec.getSelectorBuilder() != null) {
            query.multiselect(spec.getSelectorBuilder().bulid(builder, root));
        } else {
            query.select(root);
        }
        /**
         * 构造where...
         */
        Predicate predicate = spec.toPredicate(root, query, builder);
        if (predicate != null) {
            query.where(predicate);
        }

        /**
         * 构造order...
         */
        if (sort.isSorted()) {
            query.orderBy(toOrders(sort, root, builder));
        }
        TypedQuery<T> typeQuery = applyRepositoryMethodMetadataCustom(entityManager.createQuery(query));
        /**
         * 填充in查询的值
         */
        if (spec.hasInCondition()) {
            fillInfieldValues(typeQuery, spec);
        }
        return typeQuery;
    }

    /**
     * 填充in范围查询的范围值
     *
     * @param query
     * @param spec
     */
    private void fillInfieldValues(TypedQuery<?> query, ExtendedSpecification<?> spec) {
        Map<String, ParameterExpression<Collection<?>>> ipm = spec.getInParameterMap();
        Map<String, Collection<?>> ipvm = spec.getInParameterValueMap();

        Assert.notNull(ipm, "in范围查询的字段Map集合不能为null");
        Assert.notNull(ipvm, "in范围查询的字段对应的值集合不能为null!");

        ipvm.entrySet().stream().forEach(en -> query.setParameter(ipm.get(en.getKey()), en.getValue()));
    }

    /**
     * 重载方法，解析Specification，构造where... ，数量查询时才会调用
     *
     * @param spec
     * @param domainClass
     * @param query
     * @param <S>
     * @param <U>
     * @return
     */
    private <S, U extends T> Root<U> applySpecificationToCriteria(@Nullable ExtendedSpecification<U> spec, Class<U> domainClass,
                                                                  CriteriaQuery<S> query) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(query, "CriteriaQuery must not be null!");
        Root<U> root = query.from(domainClass);
        if (spec == null) {
            return root;
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);
        if (predicate != null) {
            query.where(predicate);
        }
        return root;
    }

    /**
     * 重写 SimpleJpaRepository 的数量查询，分页时会调用。
     *
     * @param spec
     * @param domainClass
     * @param <S>
     * @return
     */
    @Override
    protected <S extends T> TypedQuery<Long> getCountQuery(@Nullable Specification<S> spec, Class<S> domainClass) {

        if (!(spec instanceof ExtendedSpecification)) {
            return super.getCountQuery(spec, domainClass);
        }

        ExtendedSpecification<S> mySpec = (ExtendedSpecification<S>) spec;

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<S> root = applySpecificationToCriteria(mySpec, domainClass, query);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        // Remove all Orders the Specifications might have applied
        query.orderBy(Collections.<Order>emptyList());
        TypedQuery<Long> typeQuery = entityManager.createQuery(query);
        /**
         * 填充in查询的值
         */
        if (mySpec.hasInCondition()) {
            fillInfieldValues(typeQuery, mySpec);
        }
        return typeQuery;
    }


    /**
     * 父类 SimpleJpaRepository 的applyRepositoryMethodMetadata方法是私有的，所有自己复制一个
     *
     * @param query
     * @param <T>
     * @return
     */
    private <T> TypedQuery<T> applyRepositoryMethodMetadataCustom(TypedQuery<T> query) {
        CrudMethodMetadata metadata = getRepositoryMethodMetadata();
        if (metadata == null) {
            return query;
        }

        LockModeType type = metadata.getLockModeType();
        TypedQuery<T> toReturn = type == null ? query : query.setLockMode(type);
        applyQueryHintsCustom(toReturn);

        return toReturn;
    }

    /**
     * 父类 SimpleJpaRepository 的applyQueryHints方法是私有的，所有自己复制一个
     *
     * @param query
     */
    private void applyQueryHintsCustom(Query query) {
        for (Map.Entry<String, Object> hint : getQueryHints().withFetchGraphs(entityManager)) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }
}
