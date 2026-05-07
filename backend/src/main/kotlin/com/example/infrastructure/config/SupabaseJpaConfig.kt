package com.example.infrastructure.config

import com.example.infrastructure.persistence.ArticleSpringDataRepository
import com.example.infrastructure.persistence.UserSpringDataRepository
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.Properties
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(name = ["storage.type"], havingValue = "supabase")
@EnableJpaRepositories(
    basePackageClasses = [UserSpringDataRepository::class, ArticleSpringDataRepository::class],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
)
class SupabaseJpaConfig(
    @Value("\${SUPABASE_DB_URL}") private val dbUrl: String,
    @Value("\${SUPABASE_DB_USER}") private val dbUser: String,
    @Value("\${SUPABASE_DB_PASSWORD}") private val dbPassword: String,
) {

    @Bean
    fun dataSource(): DataSource = DataSourceBuilder.create()
        .url(dbUrl)
        .username(dbUser)
        .password(dbPassword)
        .build()

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("com.example.infrastructure.persistence")
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setJpaProperties(Properties().apply {
                setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                setProperty("hibernate.hbm2ddl.auto", "none")
                setProperty("hibernate.show_sql", "false")
            })
        }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager(entityManagerFactory)
}
