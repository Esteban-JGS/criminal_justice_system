module.exports = {
	extends: ["@commitlint/config-conventional"],
	rules: {
		// English words for types (standard conventional commits)
		"type-enum": [
			2,
			"always",
			[
				"feat",
				"fix",
				"docs",
				"chore",
				"style",
				"refactor",
				"ci",
				"test",
				"revert",
				"perf",
			],
		],
		"type-case": [2, "always", "lower-case"],
		"scope-case": [2, "always", "lower-case"],
		// We disable subject case enforcement to allow "Titulo" (starts with uppercase)
		"subject-case": [0],
		"subject-empty": [2, "never"],
		"type-empty": [2, "never"],
	},
};
