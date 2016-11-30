ALTER TABLE `character_skills` ADD `skill_sub_level` INT(4) NOT NULL DEFAULT '0' AFTER `skill_level`;
ALTER TABLE `character_skills_save` ADD `skill_sub_level` INT(4) NOT NULL DEFAULT '0' AFTER `skill_level`;
ALTER TABLE `character_summon_skills_save` ADD `skill_sub_level` INT(4) NOT NULL DEFAULT '0' AFTER `skill_level`;
ALTER TABLE `character_shortcuts` ADD `sub_level` INT(4) NOT NULL DEFAULT '0' AFTER `level`;