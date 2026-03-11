/*     */ package com.matrix2b.mwf.spring.jdbc;
/*     */ import java.io.File;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Objects;
/*     */ import java.util.Properties;

/*     */ import javax.sql.DataSource;

/*     */ import org.aopalliance.aop.Advice;
/*     */ import org.apache.commons.lang3.StringUtils;
/*     */ import org.apache.ibatis.session.ExecutorType;
/*     */ import org.apache.ibatis.session.SqlSessionFactory;
/*     */ import org.mybatis.spring.SqlSessionTemplate;
/*     */ import org.mybatis.spring.annotation.MapperScan;
/*     */ import org.springframework.aop.Advisor;
/*     */ import org.springframework.aop.Pointcut;
/*     */ import org.springframework.aop.aspectj.AspectJExpressionPointcut;
/*     */ import org.springframework.aop.support.DefaultPointcutAdvisor;
/*     */ import org.springframework.beans.factory.annotation.Autowired;
/*     */ import org.springframework.context.ApplicationContext;
/*     */ import org.springframework.context.annotation.Bean;
/*     */ import org.springframework.context.annotation.Configuration;
/*     */ import org.springframework.context.annotation.DependsOn;
/*     */ import org.springframework.context.annotation.Primary;
/*     */ import org.springframework.core.io.Resource;
/*     */ import org.springframework.jdbc.datasource.DataSourceTransactionManager;
/*     */ import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
/*     */ import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
/*     */ import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
/*     */ import org.springframework.transaction.PlatformTransactionManager;
/*     */ import org.springframework.transaction.TransactionManager;
/*     */ import org.springframework.transaction.annotation.EnableTransactionManagement;
/*     */ import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
/*     */ import org.springframework.transaction.interceptor.RollbackRuleAttribute;
/*     */ import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
/*     */ import org.springframework.transaction.interceptor.TransactionInterceptor;

/*     */ 
/*     */ import com.matrix2b.Mwf4BeanNameGenerator;
/*     */ import com.matrix2b.mwf.conf.MwfProperty;
/*     */ import com.matrix2b.mwf.mybatis.DbmsType;
/*     */ import com.matrix2b.mwf.security.KeyEncryptor;
/*     */ import com.matrix2b.mwf.spring.property.MyBatisProperty;
/*     */ import com.matrix2b.mwf.spring.property.SpringProperty;
/*     */ import com.matrix2b.mwf.util.StringUtil;
/*     */ import com.zaxxer.hikari.HikariConfig;
/*     */ import com.zaxxer.hikari.HikariDataSource;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Configuration
/*     */ @EnableTransactionManagement
/*     */ @MapperScan(basePackages = {"com.matrix2b"}, nameGenerator = Mwf4BeanNameGenerator.class)
/*     */ public class DataSourceConfig
/*     */ {
/*     */   @Autowired
/*     */   private SpringProperty springProperty;
/*     */   @Autowired
/*     */   private MyBatisProperty myBatisProperty;
/*     */   @Autowired
/*     */   private MwfProperty mwfProperty;
/*     */   @Autowired
/*     */   private KeyEncryptor keyEncryptor;
/*     */   
/*     */   private DataSource createDataSource(Map<String, String> dataSourcePropertyMap) {
/*     */     HikariDataSource hikariDataSource = null;
/*  98 */     DataSource dataSource = null;
/*  99 */     String type = dataSourcePropertyMap.get("datasource-type");
/* 100 */     if ("jndi".equalsIgnoreCase(type)) {
/* 101 */       dataSource = (new JndiDataSourceLookup()).getDataSource(dataSourcePropertyMap.get("jndi-name"));
/*     */ 
/*     */ 
/*     */     
/*     */     }
/*     */     else {
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 111 */       Properties hikariProperties = new Properties();
/* 112 */       for (String key : dataSourcePropertyMap.keySet()) {
/* 113 */         if ("datasource-type".equals(key) || "dbms-type".equals(key)) {
/*     */           continue;
/*     */         }
/* 116 */         String value = dataSourcePropertyMap.get(key);
/*     */ 
/*     */         
/* 119 */         if ("password".equals(key) && this.mwfProperty.isUseEncryptPassword()) {
/*     */           try {
/* 121 */             value = this.keyEncryptor.decryptWithUser(value, null);
/* 122 */           } catch (Exception e) {
/* 123 */             value = "";
/*     */           } 
/*     */         }
/*     */         
/* 127 */         hikariProperties.put(StringUtil.toCamelCase(key, '-'), value);
/*     */       } 
/* 129 */       hikariDataSource = new HikariDataSource(new HikariConfig(hikariProperties));
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 139 */     return (DataSource)hikariDataSource;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Bean
/*     */   public DataSource routingDataSource() {
/* 148 */     AbstractRoutingDataSource routingDataSource = new DataSourceRouter();
/*     */     
/* 150 */     Map<Object, Object> targetDataSourceMap = new HashMap<>();
/* 151 */     Map<String, Map<String, String>> allDataSourcePropertyMap = this.springProperty.getDatasource();
/* 152 */     for (String dataSourceId : allDataSourcePropertyMap.keySet()) {
/* 153 */       Map<String, String> dataSourcePropertyMap = allDataSourcePropertyMap.get(dataSourceId);
/* 154 */       targetDataSourceMap.put(dataSourceId, createDataSource(dataSourcePropertyMap));
/*     */     } 
/* 156 */     routingDataSource.setTargetDataSources(targetDataSourceMap);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 163 */     return (DataSource)routingDataSource;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @DependsOn({"routingDataSource"})
/*     */   @Bean
/*     */   public DataSource dataSource(DataSource dataSource) {
/* 174 */     return (DataSource)new LazyConnectionDataSourceProxy(dataSource);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Bean
/*     */   public RefreshableSqlSessionFactoryBean sqlSessionFactory(DataSource dataSource, ApplicationContext applicationContext) throws Exception {
/* 185 */     RefreshableSqlSessionFactoryBean sqlSessionFactoryBean = new RefreshableSqlSessionFactoryBean();
/* 186 */     sqlSessionFactoryBean.setDataSource(dataSource);
/* 187 */     sqlSessionFactoryBean.setTypeAliasesPackage(this.myBatisProperty.getTypeAliasesPackage());
/*     */ 
/*     */     
/* 190 */     String defaultDbmsType = (String)StringUtils.defaultIfBlank(this.myBatisProperty.getDbmsType(), "oracle");
/* 191 */     String[] excludDbmsTypes = (String[])Arrays.<DbmsType>stream(DbmsType.values()).filter(p -> !p.val().equals(defaultDbmsType)).map(p -> File.separator + p.val() + File.separator).toArray(x$0 -> new String[x$0]);
/* 192 */     Resource[] allMapper = applicationContext.getResources(this.myBatisProperty.getMapperLocations());
/* 193 */     Resource[] usedMapper = (Resource[])Arrays.<Resource>stream(allMapper).filter(p -> { Objects.requireNonNull(p.toString()); return (p.toString().contains(File.separator + defaultDbmsType + File.separator) || Arrays.<String>stream(excludDbmsTypes).noneMatch(p.toString()::contains)); }).toArray(x$0 -> new Resource[x$0]);
/* 194 */     sqlSessionFactoryBean.setMapperLocations(usedMapper);
/* 195 */     sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource(this.myBatisProperty.getConfigLocation()));
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 201 */     return sqlSessionFactoryBean;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Primary
/*     */   @Bean
/*     */   public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
/* 215 */     return new SqlSessionTemplate(sqlSessionFactory);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Bean
/*     */   public SqlSessionTemplate sqlSessionTemplateBatch(SqlSessionFactory sqlSessionFactory) {
/* 238 */     return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Bean
/*     */   public PlatformTransactionManager transactionManager(DataSource dataSource) {
/* 248 */     DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
/* 249 */     dataSourceTransactionManager.setDataSource(dataSource);
/* 250 */     return (PlatformTransactionManager)dataSourceTransactionManager;
/*     */   }
/*     */   
/*     */   @Bean
/*     */   public TransactionInterceptor transactionAdvice(PlatformTransactionManager transactionManager) {
/* 255 */     TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
/* 256 */     Properties transactionAttributes = new Properties();
/*     */ 
/*     */     
/* 259 */     String[] readonlyMethodNameArr = (String[])this.springProperty.getTransaction().get("readonly-method-names");
/* 260 */     if (readonlyMethodNameArr != null && readonlyMethodNameArr.length > 0) {
/* 261 */       DefaultTransactionAttribute readOnlyAttribute = new DefaultTransactionAttribute(0);
/* 262 */       readOnlyAttribute.setReadOnly(true);
/*     */       
/* 264 */       for (String readonlyMethodName : readonlyMethodNameArr) {
/* 265 */         transactionAttributes.setProperty(readonlyMethodName, readOnlyAttribute.toString());
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 271 */     RuleBasedTransactionAttribute writeAttribute = new RuleBasedTransactionAttribute(0, Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
/*     */     
/* 273 */     transactionAttributes.setProperty("*", writeAttribute.toString());
/* 274 */     transactionInterceptor.setTransactionAttributes(transactionAttributes);
/* 275 */     transactionInterceptor.setTransactionManager((TransactionManager)transactionManager);
/*     */     
/* 277 */     return transactionInterceptor; 
/*     */   }
/*     */   
/*     */   @Bean
/*     */   public Advisor transactionAdviceAdvisor(PlatformTransactionManager transactionManager) {
/* 282 */     AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
/*     */     
/* 284 */     String[] pointcutRegexpArr = (String[])this.springProperty.getTransaction().get("pointcut-regexp");
/* 285 */     if (pointcutRegexpArr != null && pointcutRegexpArr.length > 0) {
/* 286 */       pointcut.setExpression(pointcutRegexpArr[0]);
/*     */     }
/* 288 */     return (Advisor)new DefaultPointcutAdvisor((Pointcut)pointcut, (Advice)transactionAdvice(transactionManager));
/*     */   }
/*     */ }


/* Location:              D:\프로젝트자료\mwf-4.0.0.jar!\com\matrix2b\mwf\spring\jdbc\DataSourceConfig.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */