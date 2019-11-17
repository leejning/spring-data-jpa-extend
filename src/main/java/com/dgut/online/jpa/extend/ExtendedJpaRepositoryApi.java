package com.dgut.online.jpa.extend;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * @ClassName ExtendedJpaRepositoryApi
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/12 0012
 * @Version V1.0
 **/
@NoRepositoryBean
public interface ExtendedJpaRepositoryApi<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    public List<?> findAll(ExtendedSpecification<T> spec, Class<?> resultClass);

    public List<?> findAll(ExtendedSpecification<T> spec, Sort sort, Class<?> resultClass);

    public Page<?> findAll(ExtendedSpecification<T> spec, Pageable pageable, Class<?> resultClass);

    public List<T> findAll(ExtendedSpecification<T> spec);

    public List<T> findAll(ExtendedSpecification<T> spec, Sort sort);

    public Page<T> findAll(ExtendedSpecification<T> spec, Pageable pageable);
}
