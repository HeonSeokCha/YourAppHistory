package com.chs.yourapphistory.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DomainModule::class])
@ComponentScan("com.chs.yourapphistory.presentation")
class PresentationModule