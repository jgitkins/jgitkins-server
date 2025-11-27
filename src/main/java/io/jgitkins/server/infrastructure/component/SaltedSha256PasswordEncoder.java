// TODO: Have To Customization

//package io.jgitkins.server.infrastructure.component;
//
//public class SaltedSha256PasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {
//
//    @Override
//    public String encode(CharSequence rawPassword) {
//        // Usually you won't call this during login.
//        // Implement if you also want to generate "salt$hash" here.
//        throw new UnsupportedOperationException("Use your user-creation logic.");
//    }
//
//    @Override
//    public boolean matches(CharSequence rawPassword, String encodedPassword) {
//        // expected format: "sha256$<salt>$<hexHash>"
//        if (encodedPassword == null || !encodedPassword.startsWith("sha256$")) return false;
//        String[] parts = encodedPassword.split("\\$", 3);
//        if (parts.length != 3) return false;
//
//        String salt = parts[1];
//        String expectedHex = parts[2];
//        String computedHex = sha256Hex(rawPassword.toString() + salt);
//        return constantTimeEq(computedHex, expectedHex);
//    }
//
//    private static String sha256Hex(String s) {
//        try {
//            var md = java.security.MessageDigest.getInstance("SHA-256");
//            byte[] out = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
//            StringBuilder sb = new StringBuilder(out.length * 2);
//            for (byte b : out) sb.append(String.format("%02x", b));
//            return sb.toString();
//        } catch (Exception e) { throw new IllegalStateException(e); }
//    }
//
//    private static boolean constantTimeEq(String a, String b) {
//        if (a == null || b == null || a.length() != b.length()) return false;
//        int r = 0; for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
//        return r == 0;
//    }
//}