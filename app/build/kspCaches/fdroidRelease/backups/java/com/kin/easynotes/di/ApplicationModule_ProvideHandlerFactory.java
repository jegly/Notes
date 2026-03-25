package com.kin.easynotes.di;

import android.os.Handler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ApplicationModule_ProvideHandlerFactory implements Factory<Handler> {
  @Override
  public Handler get() {
    return provideHandler();
  }

  public static ApplicationModule_ProvideHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Handler provideHandler() {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideHandler());
  }

  private static final class InstanceHolder {
    static final ApplicationModule_ProvideHandlerFactory INSTANCE = new ApplicationModule_ProvideHandlerFactory();
  }
}
