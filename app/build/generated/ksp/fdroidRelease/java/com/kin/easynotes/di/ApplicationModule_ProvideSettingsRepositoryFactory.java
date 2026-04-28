package com.kin.easynotes.di;

import android.content.Context;
import com.kin.easynotes.data.repository.SettingsRepositoryImpl;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ApplicationModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private ApplicationModule_ProvideSettingsRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsRepositoryImpl get() {
    return provideSettingsRepository(contextProvider.get());
  }

  public static ApplicationModule_ProvideSettingsRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new ApplicationModule_ProvideSettingsRepositoryFactory(contextProvider);
  }

  public static SettingsRepositoryImpl provideSettingsRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideSettingsRepository(context));
  }
}
