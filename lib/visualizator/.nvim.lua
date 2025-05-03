local function save_and_run()
  vim.cmd([[wa]])
  vim.cmd([[belowright split]])
  vim.cmd([[resize -8]])
  vim.cmd([[terminal cmake -B build && cmake --build build -j 16 && ./build/mlm_visualizer]])
end

local function save_and_debug()
  vim.cmd([[wa]])
  vim.cmd([[terminal cmake -S . -B ./build && cmake --build build && gdb -q ./build/mlm_visualizer]])
end

local function save_and_run_unit_tests()
  vim.cmd([[wa]])
  vim.cmd([[belowright split]])
  vim.cmd([[resize -8]])
  vim.cmd([[terminal cmake -B build && cmake --build build -j 16 && ./build/filter/filter_unit_tests]])
end

local opts = { noremap = true, silent = true }
vim.keymap.set("n", "<C-R>", save_and_run, opts)
vim.keymap.set("n", "<C-T>", save_and_run_unit_tests, opts)
vim.keymap.set("n", "<F5>", save_and_debug, opts)
