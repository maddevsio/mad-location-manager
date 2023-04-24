local function save_and_run()
  vim.cmd([[wa]])
  vim.cmd([[belowright split]])
  vim.cmd([[resize -4]])
  vim.cmd([[terminal make run]])
  -- vim.cmd([[TermExec size=15 go_back=0 cmd="make run"]])
end

local function save_and_run_unit_tests()
  vim.cmd([[wa]])
  vim.cmd([[belowright split]])
  vim.cmd([[resize -4]])
  vim.cmd([[terminal make run_ut]])
  -- vim.cmd([[TermExec size=15 go_back=0 cmd="make run_ut"]])
end

local opts = { noremap = true, silent = true }
vim.keymap.set("n", "<C-R>", save_and_run, opts)
vim.keymap.set("n", "<C-T>", save_and_run_unit_tests, opts)
