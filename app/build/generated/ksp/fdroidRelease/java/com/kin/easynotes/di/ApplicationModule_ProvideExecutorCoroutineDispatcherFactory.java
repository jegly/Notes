package com.kin.easynotes.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.ExecutorCoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ApplicationModule_ProvideExecutorCoroutineDispatcherFactory implements Factory<ExecutorCoroutineDispatcher> {
  @Override
  public ExecutorCoroutineDispatcher get() {
    return provideExecutorCoroutineDispatcher();
  }

  public static ApplicationModule_ProvideExecutorCoroutineDispatcherFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ExecutorCoroutineDispatcher provideExecutorCoroutineDispatcher() {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideExecutorCoroutineDispatcher());
  }

  private static final class InstanceHolder {
    static final ApplicationModule_ProvideExecutorCoroutineDispatcherFactory INSTANCE = new ApplicationModule_ProvideExecutorCoroutineDispatcherFactory();
  }
}
