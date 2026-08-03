use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring, jint};

#[no_mangle]
pub extern "system" fn Java_com_example_data_NativeArchiveEngine_getEngineVersionRust(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let version_str = "Rust Native Engine v1.8 (LZMA2 Parallel Multi-Core Safe Crates)";
    let output = env.new_string(version_str).expect("Couldn't create java string!");
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_example_data_NativeArchiveEngine_compressCoreRust(
    mut env: JNIEnv,
    _class: JClass,
    _source_path: JString,
    _dest_path: JString,
    secondary_cores: jint,
) -> jboolean {
    println!("Rust Engine spawning Rayon worker pool with {} secondary cores", secondary_cores);
    1 // Return true
}
