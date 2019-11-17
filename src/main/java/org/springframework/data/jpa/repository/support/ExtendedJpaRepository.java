package org.springframework.data.jpa.repository.support;

import com.dgut.online.jpa.extend.ExtendedJpaRepositoryApi;
import com.dgut.online.jpa.extend.ExtendedSpecification;
import com.dgut.online.jpa.extend.SelectorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * @ClassName ExtendedJpaRepository
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/12 0012
 * @Version V1.0
 **/
@Slf4j
public class ExtendedJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> implements ExtendedJpaRepositoryApi<T, ID> {
    private EntityManager entityManager;
    private JpaEntityInformation<T, ?> entityInformation;

    public ExtendedJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    /**
     *
     *  下面几个查询是把结果封装到某个DTO类
     *
     */

    /**
     * @param spec
     * @param resultClass DTO类
     * @return
     */
    @Override
    public List<?> findAll(ExtendedSpecification<T> spec, Class<?> resultClass) {
        TypedQuery<Tuple> query = getQuery(spec, Sort.unsorted(), resultClass);
        return applyQueryHints(query, resultClass);
    }

    @Override
    public List<?> findAll(ExtendedSpecification<T> spec, Sort sort, Class<?> resultClass) {
        TypedQuery<Tuple> query = getQuery(spec, sort, resultClass);
        return applyQueryHints(query, resultClass);
    }

    @Override
    public Page<?> findAll(ExtendedSpecification<T> spec, Pageable pageable, Class<?> resultClass) {
        TypedQuery<Tuple> query = getQuery(spec, pageable, resultClass);
        return pageable.isUnpaged() ? new PageImpl(query.getResultList())
                : readPage(query, getDomainClass(), pageable, spec, resultClass);
    }

    protected Page<?> readPage(TypedQuery<Tuple> query, final Class<T> domainClass, Pageable pageable,
                               @Nullable Specification<T> spec, Class<?> resultClass) {
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        List<?> resultList = applyQueryHints(query, resultClass);
        return PageableExecutionUtils.getPage(resultList, pageable,
                () -> executeCountQuery(getCountQuery(spec, domainClass)));
    }

    private static long executeCountQuery(TypedQuery<Long> query) {

        Assert.notNull(query, "TypedQuery must not be null!");

        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }
        return total;
    }

    private TypedQuery<Tuple> getQuery(ExtendedSpecification<T> spec, Pageable pageable, Class<?> resultClass) {
        Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
        return getQuery(spec, sort, resultClass);
    }


    /**
     * 结果集是实体类的某个DTO类的getQuery方法重载
     *
     * @param spec
     * @param sort
     * @param resultClass DTO类
     * @return
     */
    protected TypedQuery<Tuple> getQuery(@Nullable ExtendedSpecification<T> spec, Sort sort, Class<?> resultClass) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<T> root = query.from(getDomainClass());

        SelectorBuilder selectorBuilder = spec.getSelectorBuilder();
        Assert.notNull(selectorBuilder, "SelectorBuilder选择器不能为空！");

        /**
         * 构造select...
         */
        query.multiselect(selectorBuilder.bulid(builder, root));

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
        TypedQuery<Tuple> typeQuery = entityManager.createQuery(query);
        /**
         * 填充in查询的值
         */
        if (spec.hasInCondition()) {
            fillInfieldValues(typeQuery, spec);
        }

        return typeQuery;
    }

    /**
     * 封装查询结果到某个DTO类
     *
     * @param query
     * @param resultClass DTO类
     * @return
     */
    private List<?> applyQueryHints(TypedQuery<Tuple> query, Class<?> resultClass) {
        List<Tuple> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Collections.emptyList();
        }
        Tuple tuple1 = resultList.get(0);
        List<? extends Class<?>> collect = tuple1.getElements().stream().map(t -> t.getJavaType()).collect(Collectors.toList());
        Class[] classes = collect.toArray(new Class[(collect.size())]);
        Constructor<?> constructor = null;
        try {
            constructor = resultClass.getConstructor(classes);
        } catch (NoSuchMethodException e) {
            log.error(resultClass.getName() + "——DTO类没有对应的构造方法,参数列表为：看下面");
            e.printStackTrace();
        }
        List<Object> list = Lists.newArrayList();
        for (Tuple tuple : resultList) {
            Object[] objects = new Object[collect.size()];
            for (int i = 0; i < collect.size(); i++) {
                objects[i] = tuple.get(i);
            }
            Object o = null;
            try {
                o = constructor.newInstance(objects);
            } catch (InstantiationException | InvocationTargetException e) {
                log.error("创建DTO类对象失败");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                log.error(resultClass.getName() + "——DTO类构造方法参数与结果集的字段不一致！");
                e.printStackTrace();
            }
            list.add(o);
        }
        return list;
    }


    /**
     * ================================================================================================================
     * | 下面的查询方法返回的是实体类
     * ================================================================================================================
     */

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
