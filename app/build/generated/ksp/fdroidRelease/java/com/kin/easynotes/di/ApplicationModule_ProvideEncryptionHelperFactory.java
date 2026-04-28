package com.kin.easynotes.di;

import com.kin.easynotes.presentation.components.EncryptionHelper;
import com.kin.easynotes.security.KeystoreManager;
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
public final class ApplicationModule_ProvideEncryptionHelperFactory implements Factory<EncryptionHelper> {
  private final Provider<KeystoreManager> keystoreManagerProvider;

  private ApplicationModule_ProvideEncryptionHelperFactory(
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public EncryptionHelper get() {
    return provideEncryptionHelper(keystoreManagerProvider.get());
  }

  public static ApplicationModule_ProvideEncryptionHelperFactory create(
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new ApplicationModule_ProvideEncryptionHelperFactory(keystoreManagerProvider);
  }

  public static EncryptionHelper provideEncryptionHelper(KeystoreManager keystoreManager) {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideEncryptionHelper(keystoreManager));
  }
}
