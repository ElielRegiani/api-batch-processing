package api.batch.app.job;

import api.batch.app.service.impl.ProcessBatchService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class AppJobConfig {

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);  // Definir a conexão com o banco
        factory.setTransactionManager(transactionManager); // Gerencia transações
        factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ"); // Define o nível de isolamento
        factory.afterPropertiesSet();  // Configura o JobRepository
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource); // Gerencia transações no banco de dados
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

    @Bean
    public Job appJob(JobRepository jobRepository, Step fetchDataStep, Step processBankInformationStep, Step sendProcessedDataStep) {
        return new JobBuilder("appJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fetchDataStep)  // Primeiro Step
                .next(processBankInformationStep)  // Segundo Step
                .next(sendProcessedDataStep)  // Terceiro Step
                .build();
    }

    @Bean
    public Step fetchDataStep(JobRepository jobRepository, ProcessBatchService service, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fetchDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    service.fetchDataFromAPI();  // Chama a API do Itaú
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step processBankInformationStep(JobRepository jobRepository, ProcessBatchService service, PlatformTransactionManager transactionManager) {
        return new StepBuilder("processBankInformationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    service.processData();  // Processa os dados
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step sendProcessedDataStep(JobRepository jobRepository, ProcessBatchService service, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendProcessedDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    service.sendData();  // Envia os dados processados
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
