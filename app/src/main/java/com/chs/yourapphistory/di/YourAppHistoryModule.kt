package com.chs.yourapphistory.di

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        DataModule::class,
        DomainModule::class,
        PresentationModule::class
    ]
)
@Configuration
class YourAppHistoryModule