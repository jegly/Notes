package com.kin.easynotes.di;

import com.kin.easynotes.presentation.components.EncryptionHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class ApplicationModule_ProvideEncryptionHelperFactory implements Factory<EncryptionHelper> {
  @Override
  public EncryptionHelper get() {
    return provideEncryptionHelper();
  }

  public static ApplicationModule_ProvideEncryptionHelperFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EncryptionHelper provideEncryptionHelper() {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideEncryptionHelper());
  }

  private static final class InstanceHolder {
    static final ApplicationModule_ProvideEncryptionHelperFactory INSTANCE = new ApplicationModule_ProvideEncryptionHelperFactory();
  }
}
