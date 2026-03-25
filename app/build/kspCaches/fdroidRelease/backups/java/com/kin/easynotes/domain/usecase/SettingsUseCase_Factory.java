package com.kin.easynotes.domain.usecase;

import android.content.Context;
import com.kin.easynotes.data.repository.SettingsRepositoryImpl;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SettingsUseCase_Factory implements Factory<SettingsUseCase> {
  private final Provider<Context> contextProvider;

  private final Provider<SettingsRepositoryImpl> settingsRepositoryProvider;

  private SettingsUseCase_Factory(Provider<Context> contextProvider,
      Provider<SettingsRepositoryImpl> settingsRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SettingsUseCase get() {
    return newInstance(contextProvider.get(), settingsRepositoryProvider.get());
  }

  public static SettingsUseCase_Factory create(Provider<Context> contextProvider,
      Provider<SettingsRepositoryImpl> settingsRepositoryProvider) {
    return new SettingsUseCase_Factory(contextProvider, settingsRepositoryProvider);
  }

  public static SettingsUseCase newInstance(Context context,
      SettingsRepositoryImpl settingsRepository) {
    return new SettingsUseCase(context, settingsRepository);
  }
}
