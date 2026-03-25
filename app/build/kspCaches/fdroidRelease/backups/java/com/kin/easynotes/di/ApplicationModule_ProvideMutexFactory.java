package com.kin.easynotes.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.sync.Mutex;

@ScopeMetadata
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
public final class ApplicationModule_ProvideMutexFactory implements Factory<Mutex> {
  @Override
  public Mutex get() {
    return provideMutex();
  }

  public static ApplicationModule_ProvideMutexFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Mutex provideMutex() {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideMutex());
  }

  private static final class InstanceHolder {
    static final ApplicationModule_ProvideMutexFactory INSTANCE = new ApplicationModule_ProvideMutexFactory();
  }
}
