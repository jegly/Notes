package com.kin.easynotes.di;

import android.app.Application;
import com.kin.easynotes.data.local.database.NoteDatabaseProvider;
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
public final class ApplicationModule_ProvideBackupRepositoryFactory implements Factory<ImportExportRepository> {
  private final Provider<NoteDatabaseProvider> noteDatabaseProvider;

  private final Provider<Application> applicationProvider;

  private final Provider<Mutex> mutexProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  private final Provider<ExecutorCoroutineDispatcher> executorCoroutineDispatcherProvider;

  private ApplicationModule_ProvideBackupRepositoryFactory(
      Provider<NoteDatabaseProvider> noteDatabaseProvider,
      Provider<Application> applicationProvider, Provider<Mutex> mutexProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<ExecutorCoroutineDispatcher> executorCoroutineDispatcherProvider) {
    this.noteDatabaseProvider = noteDatabaseProvider;
    this.applicationProvider = applicationProvider;
    this.mutexProvider = mutexProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
    this.executorCoroutineDispatcherProvider = executorCoroutineDispatcherProvider;
  }

  @Override
  public ImportExportRepository get() {
    return provideBackupRepository(noteDatabaseProvider.get(), applicationProvider.get(), mutexProvider.get(), coroutineScopeProvider.get(), executorCoroutineDispatcherProvider.get());
  }

  public static ApplicationModule_ProvideBackupRepositoryFactory create(
      Provider<NoteDatabaseProvider> noteDatabaseProvider,
      Provider<Application> applicationProvider, Provider<Mutex> mutexProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<ExecutorCoroutineDispatcher> executorCoroutineDispatcherProvider) {
    return new ApplicationModule_ProvideBackupRepositoryFactory(noteDatabaseProvider, applicationProvider, mutexProvider, coroutineScopeProvider, executorCoroutineDispatcherProvider);
  }

  public static ImportExportRepository provideBackupRepository(
      NoteDatabaseProvider noteDatabaseProvider, Application application, Mutex mutex,
      CoroutineScope coroutineScope, ExecutorCoroutineDispatcher executorCoroutineDispatcher) {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideBackupRepository(noteDatabaseProvider, application, mutex, coroutineScope, executorCoroutineDispatcher));
  }
}
