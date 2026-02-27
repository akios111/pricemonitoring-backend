package iee.ihu.gr.pricemonitoringbackend.configuration

import io.github.bucket4j.distributed.jdbc.BucketTableSettings
import io.github.bucket4j.distributed.jdbc.PrimaryKeyMapper
import io.github.bucket4j.distributed.jdbc.SQLProxyConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.mysql.MySQLSelectForUpdateBasedProxyManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource

@Configuration
class Bucket4jConfiguration {

    @Bean
    fun ipSqlProxyConfiguration(dataSource: DataSource) : SQLProxyConfiguration<String> = SQLProxyConfiguration
        .builder()
        .withPrimaryKeyMapper(PrimaryKeyMapper.STRING)
        .withTableSettings(BucketTableSettings.customSettings("ip_buckets","id","state"))
        .build(dataSource)

    @Bean
    fun ipProxyManager(configuration: SQLProxyConfiguration<String>) : ProxyManager<String> = MySQLSelectForUpdateBasedProxyManager(configuration)

}