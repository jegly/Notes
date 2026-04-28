package com.kin.easynotes.di;

import android.content.Context;
import com.kin.easynotes.data.repository.ImportExportRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.ExecutorCoroutineDispatcher;
import kotlinx.coroutines.sync.Mutex;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ApplicationModule_ProvideImportExportRepositoryFactory implements Factory<ImportExportRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<Mutex> mutexProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  private final Provider<ExecutorCoroutineDispatcher> executorCoroutineDispatcherProvider;

  private ApplicationModule_ProvideImportExportRepositoryFactory(Provider<Context> contextProvider,
      Provider<Mutex> mutexProvider, Provider<CoroutineScope> coroutineScopeProvider,
      Provider<ExecutorCoroutineDispatcher> executorCoroutineDispatcherProvider) {
    this.contextProvider = contextProvider;
    this.mutexProvider = mutexProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
    this.executorCoroutineDispatcherProvider = executorCoroutineDispatcherProvider;
  }

  @Override
  public ImportExportRepository get() {
    return provideImportExportRepository(contextProvider.get(), mutexProvider.get(), coroutineScopeProvider.get(), executorCoroutineDispatcherProvider.get());
  }

  public static ApplicationModule_ProvideImportExportRepositoryFactory create(
      Provider<Context> contextProvider, Provider<Mutex> mutexProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<ExecutorCoroutineDispatcher> executorCoroutineDispatcherProvider) {
    return new ApplicationModule_ProvideImportExportRepositoryFactory(contextProvider, mutexProvider, coroutineScopeProvider, executorCoroutineDispatcherProvider);
  }

  public static ImportExportRepository provideImportExportRepository(Context context, Mutex mutex,
      CoroutineScope coroutineScope, ExecutorCoroutineDispatcher executorCoroutineDispatcher) {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideImportExportRepository(context, mutex, coroutineScope, executorCoroutineDispatcher));
  }
}
