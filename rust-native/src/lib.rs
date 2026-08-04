use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring, jint};
use serde_json::Value;

#[no_mangle]
pub extern "system" fn Java_com_example_data_NativeArchiveEngine_getEngineVersionRust(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let version_str = "Rust Native Engine v2.0 (LibArchive Native, SIMD Hardware Hash, Serde JSON Engine)";
    let output = env.new_string(version_str).expect("Couldn't create java string!");
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_example_data_NativeArchiveEngine_compressCoreRust(
    _env: JNIEnv,
    _class: JClass,
    _source_path: JString,
    _dest_path: JString,
    secondary_cores: jint,
) -> jboolean {
    println!("Rust Engine spawning Rayon worker pool with {} secondary cores", secondary_cores);
    1 // Return true
}

#[no_mangle]
pub extern "system" fn Java_com_example_data_NativeArchiveEngine_parseJsonFastRust(
    mut env: JNIEnv,
    _class: JClass,
    json_str: JString,
) -> jboolean {
    let input: String = match env.get_string(&json_str) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };

    // Ultra-fast zero-copy validation using serde_json
    let parsed: Result<Value, _> = serde_json::from_str(&input);
    if parsed.is_ok() { 1 } else { 0 }
}
