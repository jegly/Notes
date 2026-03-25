package com.kin.easynotes.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.kin.easynotes.di.WidgetCoroutineScope")
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
public final class ApplicationModule_ProvidesWidgetCoroutineScopeFactory implements Factory<CoroutineScope> {
  @Override
  public CoroutineScope get() {
    return providesWidgetCoroutineScope();
  }

  public static ApplicationModule_ProvidesWidgetCoroutineScopeFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoroutineScope providesWidgetCoroutineScope() {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.providesWidgetCoroutineScope());
  }

  private static final class InstanceHolder {
    static final ApplicationModule_ProvidesWidgetCoroutineScopeFactory INSTANCE = new ApplicationModule_ProvidesWidgetCoroutineScopeFactory();
  }
}
