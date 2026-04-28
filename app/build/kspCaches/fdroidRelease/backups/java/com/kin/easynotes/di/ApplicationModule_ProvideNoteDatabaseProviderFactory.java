package com.kin.easynotes.di;

import android.app.Application;
import com.kin.easynotes.data.local.database.NoteDatabaseProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ApplicationModule_ProvideNoteDatabaseProviderFactory implements Factory<NoteDatabaseProvider> {
  private final Provider<Application> applicationProvider;

  private ApplicationModule_ProvideNoteDatabaseProviderFactory(
      Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public NoteDatabaseProvider get() {
    return provideNoteDatabaseProvider(applicationProvider.get());
  }

  public static ApplicationModule_ProvideNoteDatabaseProviderFactory create(
      Provider<Application> applicationProvider) {
    return new ApplicationModule_ProvideNoteDatabaseProviderFactory(applicationProvider);
  }

  public static NoteDatabaseProvider provideNoteDatabaseProvider(Application application) {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideNoteDatabaseProvider(application));
  }
}
