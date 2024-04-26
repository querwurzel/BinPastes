package com.github.binpastes.config;

import com.github.binpastes.BinPastes;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackageClasses = BinPastes.class)
@EnableR2dbcAuditing
public class DatabaseConfig {}
