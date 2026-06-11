package com.chs.yourapphistory.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DataModule::class])
@ComponentScan("com.chs.yourapphistory.domain")
class DomainModule