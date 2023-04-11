local function save_and_run()
  vim.cmd([[w]])
  vim.cmd([[belowright split]])
  vim.cmd([[resize -4]])
  vim.cmd([[terminal make run]])
end

local opts = { noremap = true, silent = true }
vim.keymap.set("n", "<C-R>", save_and_run, opts)
