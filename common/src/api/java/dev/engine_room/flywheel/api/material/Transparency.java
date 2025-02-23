package dev.engine_room.flywheel.api.material;

public enum Transparency {
	/**
	 * No blending. Used for solid and cutout geometry.
	 */
	OPAQUE,

	/**
	 * Additive blending.
	 *
	 * <p>Each fragment blends color and alpha with the following equation:
	 * <pre>
	 * {@code
	 * out = src + dst
	 * }
	 * </pre>
	 */
	ADDITIVE,

	/**
	 * Lightning transparency.
	 *
	 * <p>Each fragment blends color and alpha with the following equation:
	 * <pre>
	 * {@code
	 * out = src * alpha_src + dst
	 * }
	 * </pre>
	 */
	LIGHTNING,

	/**
	 * Glint transparency. Used for the enchantment effect.
	 *
	 * <p>Each fragment blends with the following equations:
	 * <pre>
	 * {@code
	 * color_out = color_src^2 + color_dst
	 * alpha_out = alpha_dst
	 * }
	 * </pre>
	 */
	GLINT,

	/**
	 * Crumbling transparency. Used for the block breaking overlay.
	 *
	 * <p>Each fragment blends with the following equations:
	 * <pre>
	 * {@code
	 * color_out = 2 * color_src * color_dst
	 * alpha_out = alpha_src
	 * }
	 * </pre>
	 */
	CRUMBLING,

	/**
	 * Translucent transparency.
	 *
	 * <p>Each fragment blends with the following equations:
	 * <pre>
	 * {@code
	 * color_out = color_src * alpha_src + color_dst * (1 - alpha_src)
	 * alpha_out = alpha_src + alpha_dst * (1 - alpha_src)
	 * }
	 * </pre>
	 */
	TRANSLUCENT,

	/**
	 * If supported by the backend, this mode will use OIT that approximates {@code TRANSLUCENT} transparency.
	 *
	 * <p>If a backend does not support OIT, it must treat this the same as {@code TRANSLUCENT}.
	 *
	 * <p>It is recommended to use this option when possible, though for cases where blend modes are used as an
	 * overlay against solid geometry the order dependent modes are preferred.
	 */
	ORDER_INDEPENDENT,
}
